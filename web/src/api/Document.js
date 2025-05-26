const BACKEND_URL = "http://localhost:8080/api/v0"; // Spring Boot Backend

export function uploadAndStream(file, onMessage) {
    const controller = new AbortController();
    const signal = controller.signal;

    const formData = new FormData();
    formData.append("file", file);

    fetch(`${BACKEND_URL}/document`, {
        method: "POST",
        body: formData,
        signal,
    }).then(response => {
        const reader = response.body.getReader();
        const decoder = new TextDecoder("utf-8");

        let buffer = "";

        function read() {
            reader.read().then(({done, value}) => {
                if (done) return;
                buffer += decoder.decode(value, {stream: true});

                const parts = buffer.split("\n\n");
                parts.slice(0, -1).forEach(event => {
                    const lines = event.split("\n");
                    const dataLine = lines.find(line => line.startsWith("data:"));
                    if (dataLine) {
                        const jsonText = dataLine.replace(/^data:\s*/, '');
                        try {
                            const json = JSON.parse(jsonText);
                            onMessage(json);
                        } catch (err) {
                            console.error("Invalid JSON in SSE stream:", jsonText, err);
                        }
                    }
                });

                buffer = parts[parts.length - 1]; // keep last incomplete chunk
                read();
            });
        }

        read();
    });

    return controller; // to cancel later if needed
}
