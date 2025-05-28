import { Button } from "@mui/material"
import styled from "styled-components"

export const Card = ({data = [], isMainPage, photo}) => {


  const CardBody = styled.article`
    position: relative;
    min-width: 200px;
    min-height: 250px;
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
      <CardBody>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        card
      </CardBody>
    )
  } else {
    return (
      <CardBody>
        {!isMainPage && (
          <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        )}
        card
      </CardBody>
    )
  }
}