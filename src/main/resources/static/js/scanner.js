// Genshin Impact Receipt Scanner Engine
// Powered by client-side OCR and server-side pattern recognition

let ocrWorker = null;

async function initOcrWorker() {
    if (typeof Tesseract === 'undefined') {
        console.warn('Tesseract.js not loaded yet');
        return null;
    }
    if (!ocrWorker) {
        ocrWorker = await Tesseract.createWorker('eng');
    }
    return ocrWorker;
}

// Process image file through OCR and send to parser API
async function scanInvoiceImage(file, onProgress, onComplete, onError) {
    try {
        if (onProgress) onProgress(10, 'Initializing OCR engine...');
        const worker = await initOcrWorker();
        
        if (!worker) {
            throw new Error('OCR engine could not be initialized.');
        }

        if (onProgress) onProgress(30, 'Reading text from image...');
        const ret = await worker.recognize(file);
        const extractedText = ret.data.text;
        
        if (onProgress) onProgress(75, 'Analyzing Genshin invoice patterns...');

        // Send extracted text to backend parser
        const response = await fetch('/api/scan/text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text: extractedText })
        });

        if (!response.ok) {
            throw new Error('Failed to parse text from server: ' + response.statusText);
        }

        const scanResult = await response.json();
        if (onProgress) onProgress(100, 'Scan complete!');
        if (onComplete) onComplete(scanResult);

    } catch (err) {
        console.error('Scan error:', err);
        if (onError) onError(err);
    }
}

// Optional: Scan directly using Gemini AI Vision
async function scanWithGeminiVision(file, onProgress, onComplete, onError) {
    try {
        if (onProgress) onProgress(20, 'Uploading image to AI Vision...');
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/scan/vision', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('AI Vision failed with status: ' + response.statusText);
        }

        const scanResult = await response.json();
        if (onProgress) onProgress(100, 'AI Vision scan complete!');
        if (onComplete) onComplete(scanResult);
    } catch (err) {
        console.error('AI Vision error:', err);
        if (onError) onError(err);
    }
}

// Check if AI vision is enabled
async function checkVisionStatus() {
    try {
        const res = await fetch('/api/scan/status');
        if (res.ok) {
            return await res.json();
        }
    } catch (e) {}
    return { visionConfigured: false, localOcrEnabled: true };
}
