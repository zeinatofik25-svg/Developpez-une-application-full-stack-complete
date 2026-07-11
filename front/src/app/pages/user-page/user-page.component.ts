import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UI_ERROR_MESSAGES } from '../../core/constants/error-messages.constants';
import { AuthService } from '../../services/auth/auth.service';
import { SubscriptionService } from '../../services/subscription/subscription.service';
import { Topic, TopicService } from '../../services/topic/topic.service';

const PASSWORD_COMPLEXITY_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).+$/;

@Component({
  selector: 'app-user-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
})
export class UserPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  protected loadingTopics = true;
  protected saving = false;
  protected error = '';
  protected successMessage = '';
  protected subscribedTopics: Topic[] = [];

  protected readonly form = this.formBuilder.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(6), Validators.pattern(PASSWORD_COMPLEXITY_REGEX)]]
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly topicService: TopicService,
    private readonly subscriptionService: SubscriptionService
  ) {}

  /**
   * Précharge les infos du profil connecté et les abonnements utilisateur.
   */
  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => {
        if (!user) {
          return;
        }

        this.form.patchValue({
          username: user.username,
          email: user.email,
          password: ''
        });
      });

    this.loadSubscribedTopics();
  }

  /**
   * Met à jour le profil (email/username/password optionnel) de l'utilisateur courant.
   */
  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.error = '';
    this.successMessage = '';

    const values = this.form.getRawValue();
    const payload = {
      email: values.email,
      username: values.username,
      ...(values.password.trim() ? { password: values.password.trim() } : {})
    };

    this.authService.updateProfile(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.saving = false;
          this.successMessage = 'Profil mis à jour.';
          this.form.patchValue({ password: '' });
        },
        error: () => {
          this.saving = false;
          this.error = UI_ERROR_MESSAGES.UPDATE_PROFILE;
        }
      });
  }

  /**
   * Désabonne l'utilisateur d'un thème depuis la page profil.
   *
   * @param topic thème à désabonner
   */
  unsubscribe(topic: Topic): void {
    this.error = '';
    this.successMessage = '';

    this.subscriptionService.unsubscribe(topic.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.subscribedTopics = this.subscribedTopics.filter(current => current.id !== topic.id);
        },
        error: () => {
          this.error = UI_ERROR_MESSAGES.UPDATE_SUBSCRIPTION;
        }
      });
  }

  /**
   * Charge uniquement les thèmes auxquels l'utilisateur est déjà abonné.
   */
  private loadSubscribedTopics(): void {
    this.loadingTopics = true;

    this.topicService.getTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.subscribedTopics = topics.filter(topic => topic.subscribed);
          this.loadingTopics = false;
        },
        error: () => {
          this.loadingTopics = false;
          this.error = UI_ERROR_MESSAGES.LOAD_SUBSCRIBED_TOPICS;
        }
      });
  }
}
