import { styled } from "styled-components";
import Button from "@mui/material/Button";
import { FileUploadButton } from "../../components/FileUploadButton";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { ReactPhotoEditor } from "react-photo-editor";
import { PhotoTemplate } from "../../components/PhotoTemplate";

export const DownloadPage = () => {
  const DownloadPageWrapper = styled.div`
    display: flex;
    justify-conten: center;
    align-items: center;
    position: relative;
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

  const StyledPhotoEditor = styled(ReactPhotoEditor)`
    position: relative;
  `;

  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate("/");
  };

  const [file, setFile] = useState();
  const [showModal, setShowModal] = useState(false);

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
    // Do something with the edited file
    console.log(editedFile);
  };

  const setFileData = (e) => {
    if (e?.target?.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };
  // пользователь отредактировал фото и отправил на загрузку
  return (
    <MainWrapper>
      <BackButton onClick={handleGoBack}>На главную</BackButton>
      <DownloadPageWrapper>
        <input type="file" onChange={(e) => setFileData(e)} multiple={false} />

        <button onClick={showModalHandler}>Edit Photo</button>
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
