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

  protected posts: PostSummary[] = [];
  protected sort: 'newest' | 'oldest' = 'newest';
  protected loading = true;
  protected error = '';
  protected isSortAscending = false;

  constructor(private readonly postService: PostService) {}

  ngOnInit(): void {
    this.loadFeed();
  }

  // Bascule le sens de tri et recharge le feed avec le nouvel ordre.
  toggleSort(): void {
    this.isSortAscending = !this.isSortAscending;
    this.sort = this.isSortAscending ? 'oldest' : 'newest';
    this.loadFeed();
  }

  // Charge la liste des posts du feed et gère les états de chargement/erreur.
  private loadFeed(): void {
    this.loading = true;
    this.error = '';

    this.postService.getFeed(this.sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: posts => {
          this.posts = posts;
          this.loading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_FEED;
          this.loading = false;
        }
      });
  }
}
