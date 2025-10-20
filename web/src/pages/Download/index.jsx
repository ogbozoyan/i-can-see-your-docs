import { styled } from "styled-components";
import Button from "@mui/material/Button";
import { FileUploadButton } from "../../components/FileUploadButton";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { ReactPhotoEditor } from "react-photo-editor";
import { PhotoTemplate } from "../../components/PhotoTemplate";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import { uploadDocument } from "../../api/Document";

export const DownloadPage = () => {
  const DownloadPageWrapper = styled.div`
    display: flex;
    flex-direction: column;
    justify-conten: center;
    align-items: center;
    position: relative;
    gap: 15px;
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
  `;

  const VisuallyHiddenInput = styled("input")({
    clip: "rect(0 0 0 0)",
    clipPath: "inset(50%)",
    height: 1,
    overflow: "hidden",
    position: "absolute",
    bottom: 0,
    left: 0,
    whiteSpace: "nowrap",
    width: 1,
  });

  const StyledButton = styled(Button)`
    width: 120px;
    height: 60px;
  `;

  const StyledPhotoEditor = styled(ReactPhotoEditor)`
    position: relative;
  `;

  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate("/");
  };

  const [file, setFile] = useState();
  const [showModal, setShowModal] = useState(false);

  // переделать сохранение
  // Show modal if file is selected
  const showModalHandler = () => {
    if (file) {
      setShowModal(true);
    }
  };

  // Hide modal
  const hideModal = () => {
    setShowModal(false);
  };

  // Save edited image
  const handleSaveImage = (editedFile) => {
    setFile(editedFile);
    handleUploadFile();
  };

  const setFileData = (e) => {
    if (e?.target?.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };

  const handleUploadFile = async () => {
    if (!file) {
      return;
    }

    const formData = new FormData();

    formData.append("file", file);
    uploadDocument(formData);
  };
  // пользователь отредактировал фото и отправил на загрузку

  return (
    <MainWrapper>
      <BackButton onClick={handleGoBack}>На главную</BackButton>
      <DownloadPageWrapper>
        <StyledButton
          component="label"
          role={undefined}
          variant="contained"
          tabIndex={-1}
          startIcon={<CloudUploadIcon />}
        >
          Загрузить файл
          <VisuallyHiddenInput
            type="file"
            onChange={(e) => setFileData(e)}
            multiple
          />
        </StyledButton>
        <Button onClick={showModalHandler} disabled={file ? false : true}>
          Отредактируйте фото
        </Button>
        <StyledPhotoEditor
          open={showModal}
          onClose={hideModal}
          file={file}
          onSaveImage={handleSaveImage}
        />

        {showModal && <PhotoTemplate />}
        {/* <FileUploadButton /> */}
      </DownloadPageWrapper>
    </MainWrapper>
  );
};
