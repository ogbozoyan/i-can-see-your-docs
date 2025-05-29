import { styled } from "styled-components";
import Button from "@mui/material/Button";
import { FileUploadButton } from "../../components/FileUploadButton";
import { useNavigate } from "react-router-dom";

export const DownloadPage = () => {
  const DownloadPageWrapper = styled.div`
    display: flex;
    justify-conten: center;
    align-items: center;
  `;
  
  const MainWrapper = styled.div`
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    min-width: 900px;
    min-height: 700px;
    border: 5px solid rgba(141, 167, 155, 0.61);
    border-radius: 25px;
  `;

  const BackButton = styled(Button)`
    position: absolute !important;
    top: 10px;
    right: 10px;
  `

  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate('/');
  }

  return (
    <MainWrapper>
      <BackButton onClick={handleGoBack}>На главную</BackButton>
      <DownloadPageWrapper>
        <FileUploadButton />
      </DownloadPageWrapper>
    </MainWrapper>
  );
};
