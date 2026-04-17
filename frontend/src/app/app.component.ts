import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatbotWidgetComponent } from './shared/chatbot-widget/chatbot-widget.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatbotWidgetComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}
