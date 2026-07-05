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

  protected topics: Topic[] = [];
  protected loading = true;
  protected error = '';

  constructor(
    private readonly topicService: TopicService,
    private readonly subscriptionService: SubscriptionService,
  ) {
  }

  ngOnInit(): void {
    this.loadTopics();
  }

  // Abonne ou désabonne l'utilisateur d'un thème puis met à jour la liste locale.
  toggleSubscription(topic: Topic): void {
    if (topic.subscribed) {
      this.subscriptionService.unsubscribe(topic.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.topics = this.topics.map(currentTopic =>
              currentTopic.id === topic.id ? { ...currentTopic, subscribed: false } : currentTopic
            );
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
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.UPDATE_SUBSCRIPTION;
        }
      });
  }

  // Charge tous les thèmes avec leur statut d'abonnement.
  private loadTopics(): void {
    this.loading = true;
    this.error = '';

    this.topicService.getTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.topics = topics;
          this.loading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_TOPICS;
          this.loading = false;
        }
      });
  }
}
