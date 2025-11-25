import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import { Card } from "../../components/Card";
import { Button, CircularProgress } from "@mui/material";
import { useEffect } from "react";
import {
  getDocs,
  getDocsFromServer,
  getIsLoading,
} from "../../store/documentsSlice";
import { BACKEND_URL } from "../../api/document.js";
import { useDispatch, useSelector } from "react-redux";
import mockPhoto from "../../mock/mockPhoto.jpg";

const isDev = import.meta.env.VITE_DEVELOPMENT;

export const MainPage = () => {
  const MainWrapper = styled.div`
    flex-wrap: wrap;
    width: 1080px;
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

  const LoadingWrapper = styled.div`
    margin: 0 auto;
  `;

  const navigate = useNavigate();

  const handleRouteToDownloadPage = () => {
    navigate("/loading");
  };

  const handleRouteToDocument = (id) => {
    navigate(`/document/${id}`);
  };

  const dispatch = useDispatch();

  useEffect(() => {
    if (!isDev) {
      dispatch(getDocsFromServer());
    }
  }, [dispatch]);

  const documents = useSelector(getDocs);
  const isLoading = useSelector(getIsLoading);

  if (isDev) {
    return (
      <MainWrapper>
        {console.log(isDev)}
        <UploadFileButtonWrapper>
          <Button onClick={handleRouteToDownloadPage}>Загрузить файл</Button>
        </UploadFileButtonWrapper>
        <CardWrapper>
          <Card
            photoLink={"s"}
            isMainPage
            handleClick={() => handleRouteToDocument(1)}
          />
        </CardWrapper>
      </MainWrapper>
    );
  }
  return (
    <MainWrapper>
      {console.log(isDev)}
      <UploadFileButtonWrapper>
        <Button onClick={handleRouteToDownloadPage}>Загрузить файл</Button>
      </UploadFileButtonWrapper>
      {!isLoading ? (
        <CardWrapper>
          {documents.map((item, idx) => (
            <Card
              key={idx}
              photoLink={`${BACKEND_URL}/presigned-url/${
                item.s3Key + item.fileName
              }`}
              isMainPage
              handleClick={() => handleRouteToDocument(item.id)}
            />
          ))}
        </CardWrapper>
      ) : (
        <LoadingWrapper>
          <CircularProgress />
        </LoadingWrapper>
      )}
    </MainWrapper>
  );
};
