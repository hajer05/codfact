import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastNotificationsComponent } from './components/notifications/toast-notifications.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastNotificationsComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'backoffice';
}
