import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Html5Qrcode } from 'html5-qrcode';
import { FeatureApiService, VerificationResponse } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-verification',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verification.component.html',
  styleUrl: './verification.component.css'
})
export class VerificationComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly featureApiService = inject(FeatureApiService);
  
  protected certCode = '';
  protected result: VerificationResponse | null = null;
  protected error = '';
  protected isScanning = false;
  private scanner: Html5Qrcode | null = null;

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      const code = params.get('code');
      if (code) {
        this.certCode = code;
        this.verify();
      }
    });
    this.startScanner();
  }

  ngOnDestroy() {
    this.stopScanner();
  }

  async startScanner(): Promise<void> {
    try {
      this.scanner = new Html5Qrcode('reader');
      this.isScanning = true;
      await this.scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: 250 },
        (decodedText) => {
          this.certCode = this.extractCode(decodedText);
          this.verify();
        },
        () => {}
      );
    } catch (e) {
      console.warn('Scanner failed to start', e);
      this.isScanning = false;
    }
  }

  async stopScanner(): Promise<void> {
    if (this.scanner && this.isScanning) {
      await this.scanner.stop();
      this.isScanning = false;
    }
  }

  verify(): void {
    if (!this.certCode) return;

    this.error = '';
    const code = this.extractCode(this.certCode);

    this.featureApiService.verifyCertificate(code).subscribe({
      next: (response) => {
        this.result = response;
        if (!response.valid) {
          this.error = 'Ce certificat n\'est pas reconnu ou a été modifié.';
        }
      },
      error: () => {
        this.result = null;
        this.error = 'Le service de vérification est momentanément indisponible.';
      }
    });
  }

  private extractCode(input: string): string {
    if (input.includes('?code=')) {
      return input.split('?code=')[1].split('&')[0];
    }
    return input.trim();
  }
}

