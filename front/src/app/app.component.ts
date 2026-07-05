import { AsyncPipe } from '@angular/common';
import { Component, HostListener, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AsyncPipe, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly currentUser$;
  protected mobileMenuOpen = false;

  constructor() {
    this.currentUser$ = this.authService.currentUser$;
    this.authService.restoreSession().subscribe();
  }

  // Termine la session utilisateur et revient à la page d'accueil.
  logout(): void {
    this.authService.logout();
    this.mobileMenuOpen = false;
    void this.router.navigate(['/']);
  }

  // Ouvre/ferme le menu mobile.
  protected toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  // Force la fermeture du menu mobile.
  protected closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  // Indique si la route active correspond aux pages d'authentification.
  protected isAuthPage(): boolean {
    const path = this.router.url.split('?')[0].split('#')[0];
    return path === '/login' || path === '/register';
  }

  @HostListener('window:resize')
  // Referme le menu si on repasse en largeur desktop.
  protected onResize(): void {
    if (window.innerWidth > 640 && this.mobileMenuOpen) {
      this.mobileMenuOpen = false;
    }
  }
}
