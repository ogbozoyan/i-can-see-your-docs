import { Button } from "@mui/material"
import styled from "styled-components"

export const Card = ({data = [], isMainPage, photo, handleClick}) => {


  const CardBody = styled.article`
    position: relative;
    width: 200px;
    height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;
  `

  const StyledRegenerateButton = styled.button`
    position: absolute;
    top: -10px;
    right: -10px;
    font-size: 10px;
  `


  if (data.length) {
    return (
      <CardBody onClick={handleClick}>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        card
      </CardBody>
    )
  } else {
    return (
      <CardBody onClick={handleClick}>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        card
      </CardBody>
    )
  }
}