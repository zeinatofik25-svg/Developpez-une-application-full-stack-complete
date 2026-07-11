import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { UI_ERROR_MESSAGES } from '../../core/constants/error-messages.constants';
import { PostService, PostSummary } from '../../services/post/post.service';

@Component({
  selector: 'app-feed-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './feed-page.component.html',
  styleUrls: ['./feed-page.component.scss']
})
export class FeedPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 4;

  protected posts: PostSummary[] = [];
  protected sort: 'newest' | 'oldest' = 'newest';
  protected loading = true;
  protected error = '';
  protected isSortAscending = false;
  protected currentPage = 0;
  protected totalPages = 0;
  protected hasNext = true;

  constructor(private readonly postService: PostService) {}

  ngOnInit(): void {
    this.loadFeed();
  }

  /**
   * Bascule le sens de tri et recharge la première page du feed.
   */
  toggleSort(): void {
    this.isSortAscending = !this.isSortAscending;
    this.sort = this.isSortAscending ? 'oldest' : 'newest';
    this.currentPage = 0;
    this.loadFeed();
  }

  /**
   * Charge la page suivante du feed.
   */
  loadNextPage(): void {
    if (!this.hasNext || this.loading || this.error) {
      return;
    }
    this.currentPage += 1;
    this.loadFeed();
  }

  /**
   * Charge la page précédente du feed.
   */
  loadPreviousPage(): void {
    if (this.currentPage === 0 || this.loading || this.error) {
      return;
    }
    this.currentPage -= 1;
    this.loadFeed();
  }

  /**
   * Indique si l'utilisateur peut charger la page précédente.
   *
   * @returns true quand la page courante est supérieure à 0
   */
  canGoPrevious(): boolean {
    return this.currentPage > 0;
  }

  /**
   * Charge la page courante du feed et met à jour les états de pagination.
   */
  private loadFeed(): void {
    this.loading = true;
    this.error = '';

    this.postService.getFeed(this.sort, this.currentPage, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          this.posts = response.items;
          this.currentPage = response.page;
          this.totalPages = response.totalPages;
          this.hasNext = response.hasNext;
          this.loading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_FEED;
          this.loading = false;
        }
      });
  }
}
