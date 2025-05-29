import styled from "styled-components"

export const CardPhoto = () => {
  const CardBody = styled.article`
    position: relative;
    width: 400px;
    height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;
  `
  return (
    <CardBody>
      photo
    </CardBody>
  )
}