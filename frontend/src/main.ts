import { bootstrapApplication } from '@angular/platform-browser';
import { importProvidersFrom } from '@angular/core';
import { App } from './app/app';
import { HttpClientModule } from '@angular/common/http';
import { MarkdownModule } from 'ngx-markdown';

bootstrapApplication(App, {
  providers: [
    importProvidersFrom(HttpClientModule),
    importProvidersFrom(MarkdownModule.forRoot())
  ]
});

