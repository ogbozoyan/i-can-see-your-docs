import styled from "styled-components";

export const CardParameters = () => {
  const CardBody = styled.article`
    position: relative;
    width: 800px;
    height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;
  `;
  const StyledRegenerateButton = styled.button`
    position: absolute;
    top: -10px;
    right: -10px;
    font-size: 10px;
  `;
  return (
    <CardBody>
      <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
      Parameters
    </CardBody>
  );
};
