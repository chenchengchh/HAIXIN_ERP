// Vosk Audio Processor for AudioWorklet
// This runs in a separate thread for better performance

class VoskAudioProcessor extends AudioWorkletProcessor {
  constructor() {
    super();
    this.recognizer = null;
    
    // Listen for messages from the main thread
    this.port.onmessage = (event) => {
      if (event.data.command === 'setRecognizer') {
        // Store the recognizer reference (this is actually a proxy in worklet)
        this.recognizer = event.data.recognizer;
      }
    };
  }

  process(inputs, outputs, parameters) {
    // Get the audio input buffer
    const input = inputs[0];
    if (input.length === 0) {
      return true;
    }

    // Get the first channel
    const channelData = input[0];
    
    // Create a Float32Array from the channel data
    const audioData = new Float32Array(channelData);
    
    // Send the audio data to the main thread for processing
    this.port.postMessage({
      type: 'audioData',
      audioData: audioData
    });
    
    // Keep the processor alive
    return true;
  }
}

// Register the processor
registerProcessor('vosk-audio-processor', VoskAudioProcessor);