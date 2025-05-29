import styled from "styled-components";
import { Card } from "../../components/Card";
import { Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { CardPhoto } from "../../components/CardPhoto";
import { CardParameters } from "../../components/CardParameters";

export const DocumentPage = () => {
  const MainWrapper = styled.div`
    position: relative;
    flex-wrap: wrap;
    max-width: 1440px;
    min-height: 800px;
    border: 5px solid rgba(141, 167, 155, 0.61);
    border-radius: 25px;
  `;

  const MainCard = styled.div`
    margin-top: 10px;
    margin-left: 30px;
    margin-bottom: 40px;
  `;

  const CardsWrapper = styled.div`
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    margin: 10px 30px;
  `;

  const BackButton = styled(Button)`
    position: absolute !important;
    top: 10px;
    right: 10px;
  `;

  const ResultWrapper = styled.div`
    display: flex;
    flex-direction: row;
    gap: 5px;
  `;

  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate("/");
  };

  return (
    <MainWrapper>
      <BackButton onClick={handleGoBack}>На главную</BackButton>
      <MainCard>
        <Card isMainPage />
      </MainCard>
      <CardsWrapper>
        <ResultWrapper>
          <CardPhoto />
          <CardParameters />
        </ResultWrapper>
        <ResultWrapper>
          <CardPhoto />
          <CardParameters />
        </ResultWrapper>
        <ResultWrapper>
          <CardPhoto />
          <CardParameters />
        </ResultWrapper>
      </CardsWrapper>
    </MainWrapper>
  );
};
