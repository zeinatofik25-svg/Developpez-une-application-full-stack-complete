import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UI_ERROR_MESSAGES } from '../../core/constants/error-messages.constants';
import { PostService } from '../../services/post/post.service';
import { Topic, TopicService } from '../../services/topic/topic.service';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.scss']
})
export class CreatePostComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  protected topics: Topic[] = [];
  protected loadingTopics = true;
  protected saving = false;
  protected error = '';

  protected readonly form = this.formBuilder.nonNullable.group({
    topicId: [0, [Validators.required, Validators.min(1)]],
    title: ['', Validators.required],
    content: ['', Validators.required]
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly topicService: TopicService,
    private readonly postService: PostService,
    private readonly router: Router
  ) {}

  /**
   * Charge la liste des thèmes abonnés disponibles pour la création d'article.
   */
  ngOnInit(): void {
    this.topicService.getTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.topics = topics.filter(topic => topic.subscribed);
          this.loadingTopics = false;
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.LOAD_TOPICS;
          this.loadingTopics = false;
        }
      });
  }

  /**
   * Crée un article puis redirige vers sa page détail.
   */
  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.error = '';

    this.postService.createPost(this.form.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: post => {
          this.saving = false;
          void this.router.navigate(['/posts', post.id]);
        },
        error: () => {
          this.saving = false;
          this.error = UI_ERROR_MESSAGES.CREATE_POST;
        }
      });
  }
}
