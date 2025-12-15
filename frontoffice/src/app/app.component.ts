import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { ToastNotificationsComponent } from './components/notifications/toast-notifications.component';
import { UniversalChatbotComponent } from './components/ai-chat-widget/universal-chatbot.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, ToastNotificationsComponent, UniversalChatbotComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'frontoffice';  

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Ensure auth service is initialized to load user from storage
    console.log('App initialized, auth service loaded');
    
    // Debug authentication state
    setTimeout(() => {
      console.log('After 1 second - Auth check:', {
        isAuthenticated: this.authService.isAuthenticated(),
        token: this.authService.getToken(),
        user: this.authService.getCurrentUser()
      });
    }, 1000);
  }
}
