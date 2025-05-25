import cv2
import numpy as np
import pytesseract
import io

TOP_MARGIN = 235
LEFT_MARGIN = 350
RIGHT_MARGIN = 295
BOTTOM_MARGIN = 355


def is_text_upright(image, keyword="СОЦИОМЕТРИЧЕСКАЯ"):
    """
    Проверяет, в правильной ли ориентации текст (по ключевому слову).
    """
    h, w = image.shape
    small = cv2.resize(image, (w // 3, h // 3))  # Ускоряем работу OCR на уменьшенной копии
    config = '--psm 6'
    text = pytesseract.image_to_string(small, lang="rus", config=config)
    return keyword in text.upper()


def crop_borders(image, top=0, left=0, right=0, bottom=0):
    """
    Обрезает края изображения.
    """
    h, w = image.shape
    y1, y2 = max(top, 0), max(min(h - bottom, h), 0)
    x1, x2 = max(left, 0), max(min(w - right, w), 0)
    return image[y1:y2, x1:x2]


def detect_skew_angle(image):
    """
    Детектит угол наклона текста с помощью преобразования Хафа.
    Возвращает угол в градусах.
    """
    # Бинаризация — превращаем изображение в чёрно-белое
    _, binary = cv2.threshold(image, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    # Детектим контуры (границы) текста
    edges = cv2.Canny(binary, 50, 150, apertureSize=3)
    # Hough Transform — поиск прямых
    lines = cv2.HoughLines(edges, 1, np.pi / 180, threshold=200)

    angles = []
    if lines is not None:
        for line in lines:
            rho, theta = line[0]
            degree = np.rad2deg(theta)
            # Берём только "почти вертикальные" или "почти горизонтальные" линии
            # Т.к. текст идёт горизонтально, нам нужны линии, близкие к 90 градусам
            if 80 < degree < 100 or 260 < degree < 280:
                angle = (theta - np.pi / 2) * (180 / np.pi)  # Переводим в градусы
                angles.append(angle)
    if angles:
        skew_angle = np.mean(angles)
        # Не даём корректировать если угол аномальный (например, больше 15°)
        if abs(skew_angle) > 15:
            return 0
        return skew_angle
    return 0


def deskew_hough(image_bytes: bytes, keyword="СОЦИОМЕТРИЧЕСКАЯ"):
    """
    Автоматически выравнивает изображение по тексту, если завалено.
    Принимает байты изображения.
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)

    if image is None:
        raise ValueError("Не удалось декодировать изображение из байтов")

    angle = detect_skew_angle(image)
    if abs(angle) > 0.2:  # Корректируем только если завалено заметно (например, >0.5°)
        (h, w) = image.shape
        center = (w // 2, h // 2)
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        rotated_img = cv2.warpAffine(image, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
    else:
        rotated_img = image  # Не было наклона

    # Проверяем, что ключевое слово читается (и, если нет, пробуем повернуть)
    if not is_text_upright(rotated_img, keyword=keyword):
        # Пробуем разные углы, если вдруг изображение перевёрнуто
        for test_angle in [90, -90, 180]:
            M = cv2.getRotationMatrix2D((rotated_img.shape[1] // 2, rotated_img.shape[0] // 2), test_angle, 1.0)
            test_rotated = cv2.warpAffine(rotated_img, M, (rotated_img.shape[1], rotated_img.shape[0]),
                                          flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
            if is_text_upright(test_rotated, keyword=keyword):
                rotated_img = test_rotated
                angle += test_angle
                break

        # Обрезаем края
        rotated_img = crop_borders(rotated_img, **dict(top=TOP_MARGIN, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN))

    return rotated_img


def crop_regions(img, regions):
    """
    img       — NumPy-массив (после deskew_hough)
    regions   — dict с ключами-именами и значениями-словарями
                {'top':…, 'left':…, 'right':…, 'bottom':…}
    возвращает dict {region_name: cropped_image_array}
    """
    crops = {}
    h, w = img.shape
    for name, p in regions.items():
        y1 = max(0, p['top'])
        y2 = min(h, h - p.get('bottom', 0))
        x1 = max(0, p['left'])
        x2 = min(w, w - p.get('right', 0))
        crops[name] = img[y1:y2, x1:x2]
    return crops


def parser(image_bytes: bytes):
    """
    Принимает байты изображения для обработки.
    """
    img = deskew_hough(image_bytes) # Pass bytes directly

    regions = {
        "table_1": dict(top=TOP_MARGIN + 535, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 1950),
        "table_1_2": dict(top=TOP_MARGIN + 835, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 1770),
        "table_2_1": dict(top=TOP_MARGIN + 1125, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 1495),
        "table_2_2": dict(top=TOP_MARGIN + 1275, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 1310),
        "table_3_1": dict(top=TOP_MARGIN + 1470, left=LEFT_MARGIN + 50, right=RIGHT_MARGIN + 265,
                          bottom=BOTTOM_MARGIN + 1155),
        "table_3_2": dict(top=TOP_MARGIN + 1632, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 980),
        "table_4_1": dict(top=TOP_MARGIN + 1831, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 805),
        "table_4_2": dict(top=TOP_MARGIN + 1987, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 630),
        "table_5_1": dict(top=TOP_MARGIN + 2173, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 455),
        "table_5_2": dict(top=TOP_MARGIN + 2345, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 280),
        "last_number": dict(top=TOP_MARGIN + 2555, left=LEFT_MARGIN, right=RIGHT_MARGIN, bottom=BOTTOM_MARGIN + 20),
    }

    return crop_regions(img, regions)