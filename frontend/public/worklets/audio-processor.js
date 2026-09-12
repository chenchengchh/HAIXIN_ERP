class AudioProcessor extends AudioWorkletProcessor {
  constructor() {
    super();
    this.port.onmessage = (event) => {
      // Handle messages from the main thread if needed
    };
  }

  process(inputs, outputs, parameters) {
    const input = inputs[0];
    if (input && input.length > 0) {
      const channel = input[0];
      // Send audio data to the main thread
      this.port.postMessage({
        type: 'audioData',
        data: channel.slice(0)
      });
    }
    return true; // Keep the processor alive
  }
}

registerProcessor('audio-processor', AudioProcessor);
