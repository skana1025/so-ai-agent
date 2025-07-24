import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private readonly apiUrl = '/api/ask';

  constructor(private http: HttpClient) {}

  async askQuestion(question: string): Promise<{ answer: string }> {
    return await firstValueFrom(this.http.post<{ answer: string }>(this.apiUrl, { question }));
  }
}