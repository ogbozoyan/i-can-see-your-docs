import { Outlet } from "react-router-dom"
import styled from "styled-components"
import { FileUploadButton } from "../../components/FileUploadButton"
import { Card } from "../../components/Card"

export const MainPage = () => {
  const MainWrapper = styled.div`
        flex-wrap: wrap;
        min-width: 1440px;
        min-height: 800px;
        border: 5px solid rgba(141, 167, 155, 0.61); 
        border-radius: 25px;
    `

    const UploadFileButtonWrapper = styled.div`
        display: flex;
        justify-self: end;
        margin-right: 10px;
        margin-top: 10px;
    `

    const CardWrapper = styled.div`
        margin-top: 20px;
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        margin-left: 20px;
    `
    // const [uploads, setUploads] = useState([]);
    // const [uploading, setUploading] = useState(false);

    // function handleFileChange(event) {
    //     const file = event.target.files[0];
    //     if (!file) return;

    //     setUploading(true);
    //     setUploads([]);

    //     uploadAndStream(file, (chunk) => {
    //         setUploads(prev => [...prev, chunk]);
    //     });
    // }

    // return (
    //     <div className="App">
    //         <h2>Upload Document</h2>
    //         <input type="file" onChange={handleFileChange} disabled={uploading}/>
    //         <div>
    //             {uploads.map(({name, url}, index) => (
    //                 <UploadedFileItem key={index} name={name} url={url}/>
    //             ))}
    //         </div>
    //     </div>
    // );

    return (

        <MainWrapper>
            <UploadFileButtonWrapper>
                <FileUploadButton />
            </UploadFileButtonWrapper>
            <CardWrapper>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>
                <Card/>

            </CardWrapper>
        </MainWrapper>
    )
}
