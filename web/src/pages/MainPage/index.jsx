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

    return (

        <MainWrapper>
            <UploadFileButtonWrapper>
                <FileUploadButton />
            </UploadFileButtonWrapper>
            <CardWrapper>
                <Card isMainPage={true}/>
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
