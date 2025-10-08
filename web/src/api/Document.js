

export const BACKEND_URL = import.meta.env.VITE_API_BACKEND;

export const getDocuments = () =>
  fetch(`${BACKEND_URL}/document`)
    .then((response) => response.json())
    .then((data) => {
      if (data) {
        return data;
      }

      return Promise.reject(data);
    });

    // TODO: рефактор
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
