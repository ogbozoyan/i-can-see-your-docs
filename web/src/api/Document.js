export const BACKEND_URL = "http://localhost:8080/api/v0"; // Spring Boot Backend

export function uploadAndStream(file, onMessage) {
  const controller = new AbortController();
  const signal = controller.signal;

  const formData = new FormData();
  formData.append("file", file);

  fetch(`${BACKEND_URL}/document`, {
    method: "POST",
    body: formData,
    signal,
  }).then((response) => {
    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");

    let buffer = "";

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) return;
        buffer += decoder.decode(value, { stream: true });

        const parts = buffer.split("\n\n");
        parts.slice(0, -1).forEach((event) => {
          const lines = event.split("\n");
          const dataLine = lines.find((line) => line.startsWith("data:"));
          if (dataLine) {
            const jsonText = dataLine.replace(/^data:\s*/, "");
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

export const getDocuments = () =>
  fetch(`${BACKEND_URL}/document`)
    .then((response) => response.json())
    .then((data) => {
      if (data) {
        return data;
      }

      return Promise.reject(data);
    });

export async function uploadDocument(file) {
  try {
    // Шаг 1
    const uploadResponse = await fetch(`${BACKEND_URL}/document`, {
      method: "POST",
      body: file,
    });

    if (!uploadResponse.ok) throw new Error("Ошибка при загрузке файла");
    const { id: uuid } = await uploadResponse.json();

    // Шаг 2
    await fetch(`${BACKEND_URL}/document/split?uuid=${uuid}`);

    // Шаг 3
    await fetch(`${BACKEND_URL}/document/process?uuid=${uuid}`);

    // Получение результата
    const finalResponse = await fetch(`${BACKEND_URL}/document/${uuid}`);
    const document = await finalResponse.json();

    console.log("Document processed:", document);
    // отрисуй document
  } catch (err) {
    console.error("Ошибка во время выполнения цепочки:", err);
  }
}

export const getDocumentByUuid = (id) =>
  fetch(`${BACKEND_URL}/document/${id}`)
    .then((response) => response.json())
    .then((data) => data);
/**
 * 1) отправляю первый шаг -> получаю успех и uuid
 * 2) отправляю второй шаг после первого, сразу после второй третий шаг
 * 3) начинаю отрисовывать документ по методу get by uuid
 */
