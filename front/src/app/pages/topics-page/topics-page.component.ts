import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { UI_ERROR_MESSAGES } from '../../core/constants/error-messages.constants';
import { SubscriptionService } from '../../services/subscription/subscription.service';
import { Topic, TopicService } from '../../services/topic/topic.service';

@Component({
  selector: 'app-topics-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './topics-page.component.html',
  styleUrls: ['./topics-page.component.scss']
})
export class TopicsPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 4;

  protected topics: Topic[] = [];
  protected pagedTopics: Topic[] = [];
  protected loading = true;
  protected error = '';
  protected currentPage = 0;
  protected totalPages = 0;

  constructor(
    private readonly topicService: TopicService,
    private readonly subscriptionService: SubscriptionService,
  ) {
  }

  ngOnInit(): void {
    this.loadTopics();
  }

  /**
   * Abonne ou désabonne l'utilisateur d'un thème puis met à jour la liste locale.
   *
   * @param topic thème ciblé
   */
  toggleSubscription(topic: Topic): void {
    if (topic.subscribed) {
      this.subscriptionService.unsubscribe(topic.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.topics = this.topics.map(currentTopic =>
              currentTopic.id === topic.id ? { ...currentTopic, subscribed: false } : currentTopic
            );
            this.applyPagination();
          },
          error: () => {
            this.error = UI_ERROR_MESSAGES.UPDATE_SUBSCRIPTION;
          }
        });
      return;
    }

    this.subscriptionService.subscribe(topic.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updatedTopic => {
          this.topics = this.topics.map(currentTopic =>
            currentTopic.id === topic.id ? updatedTopic : currentTopic
          );
          this.applyPagination();
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.UPDATE_SUBSCRIPTION;
        }
      });
  }

  /**
   * Charge la page suivante des thèmes.
   */
  loadNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }
    this.currentPage += 1;
    this.applyPagination();
  }

  /**
   * Charge la page précédente des thèmes.
   */
  loadPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }
    this.currentPage -= 1;
    this.applyPagination();
  }

  /**
   * Indique si la navigation vers la page précédente est possible.
   *
   * @returns true quand page > 0
   */
  canGoPrevious(): boolean {
    return this.currentPage > 0;
  }

  /**
   * Indique si la navigation vers la page suivante est possible.
   *
   * @returns true quand une page suivante existe
   */
  canGoNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }

  /**
   * Charge tous les thèmes avec leur statut d'abonnement.
   */
  private loadTopics(): void {
    this.loading = true;
    this.error = '';

    this.topicService.getTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.topics = topics;
          this.currentPage = 0;
          this.applyPagination();
          this.loading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_TOPICS;
          this.loading = false;
        }
      });
  }

  /**
   * Applique la pagination locale de 4 cartes par page.
   */
  private applyPagination(): void {
    this.totalPages = Math.ceil(this.topics.length / this.pageSize);

    if (this.totalPages === 0) {
      this.currentPage = 0;
      this.pagedTopics = [];
      return;
    }

    if (this.currentPage > this.totalPages - 1) {
      this.currentPage = this.totalPages - 1;
    }

    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.pagedTopics = this.topics.slice(start, end);
  }
}
