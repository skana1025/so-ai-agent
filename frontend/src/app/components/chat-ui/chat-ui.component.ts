import { Component, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { AiService } from '../../services/ai.service';

@Component({
  selector: 'app-chat-ui',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './chat-ui.component.html',
  styleUrls: ['./chat-ui.component.scss']
})
export class ChatUiComponent {
  @ViewChild('messageEnd') private messagesEndRef!: ElementRef;

  input = '';
  loading = false;

  messages: { from: 'user' | 'ai', text: string }[] = [
    { from: 'ai', text: 'Hi! Ask me anything about Solution Office.' }
  ];

  constructor(private ai: AiService) {}


  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom() {
    if (this.messagesEndRef) {
      this.messagesEndRef.nativeElement.scrollIntoView({ behavior: 'smooth' });
    }
  }

  async send() {
    if (!this.input.trim()) return;

    const question = this.input;
    this.messages.push({ from: 'user', text: question });
    this.input = '';
    this.loading = true;

    try {
      const response = await this.ai.askQuestion(question);
      this.messages.push({ from: 'ai', text: response.answer });
    } catch (error) {
      this.messages.push({ from: 'ai', text: '⚠️ Something went wrong. Please try again.' });
    } finally {
      this.loading = false;
    }
  }
}