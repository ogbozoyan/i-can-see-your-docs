import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { Card } from "../../components/Card";
import { Button } from "@mui/material";

export const MainPage = () => {
  const MainWrapper = styled.div`
    flex-wrap: wrap;
    max-width: 1440px;
    min-height: 800px;
    border: 5px solid rgba(141, 167, 155, 0.61);
    border-radius: 25px;
  `;

  const UploadFileButtonWrapper = styled.div`
    display: flex;
    justify-self: end;
    margin-right: 10px;
    margin-top: 10px;
  `;

  const CardWrapper = styled.div`
    margin: 40px 20px;
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
  `;

  const navigate = useNavigate();

  const handleRouteToDownloadPage = () => {
    navigate("/loading");
  };

  const handleRouteToDocument = (evt) => {
    navigate(`/document/${evt.clientX}`);
  };

  return (
    <MainWrapper>
      <UploadFileButtonWrapper>
        <Button onClick={handleRouteToDownloadPage}>Загрузить файл</Button>
      </UploadFileButtonWrapper>
      <CardWrapper>
        <Card isMainPage handleClick={handleRouteToDocument} />
        <Card isMainPage handleClick={handleRouteToDocument} />
        <Card isMainPage handleClick={handleRouteToDocument} />
        <Card isMainPage handleClick={handleRouteToDocument} />
        <Card isMainPage handleClick={handleRouteToDocument} />
      </CardWrapper>
    </MainWrapper>
  );
};
