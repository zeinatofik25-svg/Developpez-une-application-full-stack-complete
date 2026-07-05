import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UI_ERROR_MESSAGES } from '../../core/constants/error-messages.constants';
import { CommentService } from '../../services/comment/comment.service';
import { PostDetail, PostService } from '../../services/post/post.service';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss']
})
export class PostDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  protected post: PostDetail | null = null;
  protected loading = true;
  protected error = '';
  protected commentLoading = false;
  protected readonly commentForm = this.formBuilder.nonNullable.group({
    content: ['', Validators.required]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly formBuilder: FormBuilder,
    private readonly postService: PostService,
    private readonly commentService: CommentService,
  ) {}

  // Charge le détail de l'article depuis l'id de route (postId).
  ngOnInit(): void {
    const postId = Number(this.route.snapshot.paramMap.get('postId'));
    if (!postId) {
      this.loading = false;
      this.error = UI_ERROR_MESSAGES.NOT_FOUND_POST;
      return;
    }

    this.postService.getPostDetail(postId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: post => {
          this.post = post;
          this.loading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_POST;
          this.loading = false;
        }
      });
  }

  // Envoie un nouveau commentaire et l'ajoute localement à la liste affichée.
  submitComment(): void {
    if (!this.post || this.commentForm.invalid || this.commentLoading) {
      this.commentForm.markAllAsTouched();
      return;
    }

    this.commentLoading = true;
    this.commentService.createComment(this.post.id, this.commentForm.getRawValue().content)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: comment => {
          this.post = {
            ...this.post!,
            comments: [...this.post!.comments, comment]
          };
          this.commentForm.reset({ content: '' });
          this.commentLoading = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.CREATE_COMMENT;
          this.commentLoading = false;
        }
      });
  }
}
