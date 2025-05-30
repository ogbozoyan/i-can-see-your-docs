import { useEffect, useState } from "react";
import styled from "styled-components";

export const CardPhoto = ({ photoLink }) => {
  const CardBody = styled.article`
    position: relative;
    width: 400px;
    height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;
  `;

  const StyledImg = styled.img`
    width: 100%;
    object-fit: contain;
  `;

  const [imgSrc, setImgSrc] = useState("");

  useEffect(() => {
    if (!photoLink) return;
    fetch(photoLink)
      .then((response) => response.text())
      .then((text) => setImgSrc(text))
      .catch((err) => console.error("Ошибка загрузки изображения:", err));
  }, [photoLink]);

  return (
    <CardBody>
      {console.log(imgSrc)}
      <StyledImg src={imgSrc} alt="фото" />
    </CardBody>
  );
};
