import { styled } from "@mui/material/styles";
import Button from "@mui/material/Button";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import { uploadDocument } from "../../api/document.js";

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

export const FileUploadButton = () => {
  const handleUploadFile = async (evt) => {
    const selectedFile = evt.target.files[0];

    if (!selectedFile) {
      return;
    }

    const formData = new FormData();

    formData.append("file", selectedFile);
    console.log(selectedFile);

    const response = uploadDocument(formData);
    console.log(response);
  };

  return (
    <StyledButton
      component="label"
      role={undefined}
      variant="contained"
      tabIndex={-1}
      startIcon={<CloudUploadIcon />}
    >
      Загрузить файл
      <VisuallyHiddenInput type="file" onChange={handleUploadFile} multiple />
    </StyledButton>
  );
};
