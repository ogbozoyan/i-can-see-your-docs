import { useEffect, useState } from "react";
import styled from "styled-components";

const isDev = import.meta.env.VITE_DEVELOPMENT;
export const Card = ({
  data = [],
  isMainPage,
  photoLink,
  handleClick,
  idx,
}) => {
  const [imgSrc, setImgSrc] = useState("");
  console.log(photoLink);

  // TODO: рефактор через стор
  useEffect(() => {
    fetch(photoLink)
      .then((response) => response.text())
      .then((text) => setImgSrc(text));
  }, [photoLink]);
  const CardBody = styled.article`
    position: relative;
    width: 200px;
    height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;

    &:hover {
      cursor: pointer;
    }
  `;

  const StyledRegenerateButton = styled.button`
    position: absolute;
    top: -10px;
    right: -10px;
    font-size: 10px;
  `;

  const StyledImg = styled.img`
    max-width: 100%;
    object-fit: cover;
  `;

  const [imgSrc, setImgSrc] = useState("");
  console.log(photoLink);

  useEffect(() => {
    if (!isDev) {
      fetch(photoLink)
        .then((response) => response.text())
        .then((text) => setImgSrc(text));
    }
  }, [photoLink]);
  if (data.length) {
    return (
      <CardBody onClick={handleClick}>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        <StyledImg src={imgSrc} alt="фото" />
      </CardBody>
    );
  } else {
    return (
      <CardBody onClick={handleClick}>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        <StyledImg src={imgSrc} alt="фото" />
      </CardBody>
    );
  }
};
