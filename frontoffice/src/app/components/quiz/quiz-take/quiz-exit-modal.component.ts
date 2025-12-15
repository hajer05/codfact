import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-quiz-exit-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-overlay" (click)="onCancel()">
      <div class="modal-container" (click)="$event.stopPropagation()">
        <!-- Header -->
        <div class="modal-header">
          <div class="warning-icon">
            <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z">
              </path>
            </svg>
          </div>
          <h2 class="modal-title">🚨 QUIZ EN COURS</h2>
          <p class="modal-subtitle">Navigation Bloquée</p>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <div class="warning-message">
            <p class="main-warning">⚠️ Vous avez un quiz en cours !</p>
          </div>

          <div class="consequences-list">
            <p class="consequences-title">Si vous quittez maintenant :</p>
            <div class="consequence-item">
              <span class="icon-cross">❌</span>
              <span>Toutes vos réponses seront <strong>PERDUES</strong></span>
            </div>
            <div class="consequence-item">
              <span class="icon-cross">❌</span>
              <span>Le quiz sera marqué comme <strong>ABANDONNÉ</strong></span>
            </div>
            <div class="consequence-item">
              <span class="icon-cross">❌</span>
              <span>Vous devrez <strong>tout recommencer</strong></span>
            </div>
            <div class="consequence-item">
              <span class="icon-stop">⏹️</span>
              <span>Le chronomètre sera <strong>arrêté</strong></span>
            </div>
          </div>

          <div class="tip-box">
            <span class="tip-icon">💡</span>
            <p>Utilisez le bouton <strong>"Quitter le Quiz"</strong> en haut de la page si vous voulez vraiment abandonner.</p>
          </div>

          <div class="final-question">
            <p>Voulez-vous vraiment quitter et perdre votre progression ?</p>
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button class="btn-stay" (click)="onCancel()">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            Rester et Continuer
          </button>
          <button class="btn-leave" (click)="onConfirm()">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            Quitter Quand Même
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
      animation: fadeIn 0.2s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .modal-container {
      background: white;
      border-radius: 24px;
      max-width: 600px;
      width: 90%;
      max-height: 90vh;
      overflow-y: auto;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
      animation: slideUp 0.3s ease-out;
    }

    @keyframes slideUp {
      from {
        transform: translateY(20px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }

    .modal-header {
      background: linear-gradient(135deg, #dc2626 0%, #991b1b 100%);
      color: white;
      padding: 2rem;
      text-align: center;
      border-radius: 24px 24px 0 0;
    }

    .warning-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      margin-bottom: 1rem;
      animation: pulse 2s ease-in-out infinite;
    }

    @keyframes pulse {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.05); }
    }

    .warning-icon svg {
      color: white;
    }

    .modal-title {
      font-size: 1.75rem;
      font-weight: 800;
      margin: 0.5rem 0;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .modal-subtitle {
      font-size: 1rem;
      opacity: 0.9;
      font-weight: 600;
      margin: 0;
    }

    .modal-body {
      padding: 2rem;
    }

    .warning-message {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      border-left: 4px solid #f59e0b;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
    }

    .main-warning {
      font-size: 1.125rem;
      font-weight: 700;
      color: #92400e;
      margin: 0;
    }

    .consequences-list {
      background: #fef2f2;
      border: 2px solid #fecaca;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }

    .consequences-title {
      font-size: 1rem;
      font-weight: 700;
      color: #991b1b;
      margin: 0 0 1rem 0;
    }

    .consequence-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 0;
      font-size: 0.95rem;
      color: #7f1d1d;
    }

    .icon-cross, .icon-stop {
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .tip-box {
      background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
      border-left: 4px solid #3b82f6;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      display: flex;
      align-items: start;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }

    .tip-icon {
      font-size: 1.5rem;
      flex-shrink: 0;
    }

    .tip-box p {
      margin: 0;
      color: #1e3a8a;
      font-size: 0.95rem;
      line-height: 1.5;
    }

    .final-question {
      text-align: center;
      padding: 1rem;
      background: #f9fafb;
      border-radius: 12px;
      border: 2px dashed #d1d5db;
    }

    .final-question p {
      margin: 0;
      font-size: 1.125rem;
      font-weight: 700;
      color: #374151;
    }

    .modal-footer {
      padding: 1.5rem 2rem 2rem;
      display: flex;
      gap: 1rem;
      flex-direction: column-reverse;
    }

    @media (min-width: 640px) {
      .modal-footer {
        flex-direction: row;
      }
    }

    .btn-stay, .btn-leave {
      flex: 1;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      font-weight: 700;
      font-size: 1rem;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      transition: all 0.2s ease;
    }

    .btn-stay {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: white;
      box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3);
    }

    .btn-stay:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
    }

    .btn-leave {
      background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
      color: white;
      box-shadow: 0 4px 6px -1px rgba(107, 114, 128, 0.3);
    }

    .btn-leave:hover {
      background: linear-gradient(135deg, #dc2626 0%, #991b1b 100%);
      transform: translateY(-2px);
      box-shadow: 0 10px 15px -3px rgba(220, 38, 38, 0.4);
    }

    .btn-stay:active, .btn-leave:active {
      transform: translateY(0);
    }

    strong {
      font-weight: 800;
    }
  `]
})
export class QuizExitModalComponent {
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
