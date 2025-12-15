import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface ChatMessage {
  id: string;
  content: string;
  isUser: boolean;
  timestamp: Date;
  isLoading?: boolean;
}

@Component({
  selector: 'app-universal-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './universal-chatbot.component.html',
  styleUrls: ['./universal-chatbot.component.scss']
})
export class UniversalChatbotComponent implements OnInit, AfterViewChecked, OnDestroy {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  isOpen = false;
  currentMessage = '';
  messages: ChatMessage[] = [];
  isLoading = false;

  private apiUrl = 'http://localhost:8090/api/chatbot/universal';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    this.initializeChat();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  ngOnDestroy() {
    // Cleanup if needed
  }

  private initializeChat() {
    this.messages = [
      {
        id: '1',
        content: "👋 Bonjour ! Je suis l'assistant IA de CODING FACTORY. Je peux vous aider avec tout ce qui concerne la plateforme : cours, PFE, consulting, paiements, progression, certificats, et bien plus encore. Comment puis-je vous aider aujourd'hui ?",
        isUser: false,
        timestamp: new Date()
      }
    ];
  }

  openChat() {
    this.isOpen = true;
  }

  closeChat() {
    this.isOpen = false;
  }

  async sendMessage() {
    if (!this.currentMessage.trim() || this.isLoading) {
      return;
    }

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      content: this.currentMessage.trim(),
      isUser: true,
      timestamp: new Date()
    };

    this.messages.push(userMessage);
    const messageToSend = this.currentMessage.trim();
    this.currentMessage = '';
    this.isLoading = true;

    const loadingMessage: ChatMessage = {
      id: (Date.now() + 1).toString(),
      content: 'Réflexion en cours...',
      isUser: false,
      timestamp: new Date(),
      isLoading: true
    };
    this.messages.push(loadingMessage);

    try {
      const response = await this.http.post<{response: string}>(this.apiUrl, {
        message: messageToSend
      }).toPromise();

      this.messages = this.messages.filter(msg => !msg.isLoading);
      
      const aiMessage: ChatMessage = {
        id: (Date.now() + 2).toString(),
        content: response?.response || 'Désolé, je ne peux pas répondre pour le moment.',
        isUser: false,
        timestamp: new Date()
      };
      this.messages.push(aiMessage);
    } catch (error) {
      console.error('Erreur chatbot universel:', error);
      
      this.messages = this.messages.filter(msg => !msg.isLoading);
      
      const errorMessage: ChatMessage = {
        id: (Date.now() + 2).toString(),
        content: 'Désolé, je rencontre un problème technique. Veuillez réessayer ou contacter le support.',
        isUser: false,
        timestamp: new Date()
      };
      this.messages.push(errorMessage);
    } finally {
      this.isLoading = false;
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom() {
    if (this.messagesContainer) {
      try {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
      } catch (err) {
        console.error('Erreur scroll:', err);
      }
    }
  }
}

