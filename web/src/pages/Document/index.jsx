import styled from "styled-components";
import { Card } from "../../components/Card";
import { Button } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { CardPhoto } from "../../components/CardPhoto";
import { CardParameters } from "../../components/CardParameters";
import { useEffect, useState } from "react";
import { getDocumentByUuid } from "../../api/document";
import { BACKEND_URL } from "../../api/document";

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

  const { id } = useParams();
  const [data, setData] = useState();

  useEffect(() => {
    const fetchDocument = async () => {
      try {
        const dataDoc = await getDocumentByUuid(id);
        console.log(dataDoc);
        setData(dataDoc);
      } catch (err) {
        console.error("Ошибка загрузки документа:", err);
      }
    };

    if (id) {
      fetchDocument();
    }
  }, [id]);

  const handleGoBack = () => {
    navigate("/");
  };

  return (
    <MainWrapper>
      <BackButton onClick={handleGoBack}>На главную</BackButton>
      <MainCard>
        {data?.table_1_url ? (
          <Card
            isMainPage
            photoLink={`${BACKEND_URL}/presigned-url/${
              data.s3Key + data.fileName
            }`}
          />
        ) : (
          <div>Загрузка изображения...</div>
        )}
      </MainCard>
      <CardsWrapper>
        {/* table_1 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_1_url}`}
          />
          <CardParameters data={data?.table_1_Result} />
        </ResultWrapper>

        {/* table_1_2 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_1_2_url}`}
          />
          <CardParameters data={data?.table_1_2_Result} />
        </ResultWrapper>

        {/* table_2_1 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_2_1_url}`}
          />
          <CardParameters data={data?.table_2_1_result} />
        </ResultWrapper>

        {/* table_2_2 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_2_2_url}`}
          />
          <CardParameters data={data?.table_2_2_Result} />
        </ResultWrapper>

        {/* table_3_1 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_3_1_url}`}
          />
          <CardParameters data={data?.table_3_1_result} />
        </ResultWrapper>

        {/* table_3_2 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_3_2_url}`}
          />
          <CardParameters data={data?.table_3_2_result} />
        </ResultWrapper>

        {/* table_4_1 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_4_1_url}`}
          />
          <CardParameters data={data?.table_4_1_result} />
        </ResultWrapper>

        {/* table_4_2 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_4_2_url}`}
          />
          <CardParameters data={data?.table_4_2_result} />
        </ResultWrapper>

        {/* table_5_1 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_5_1_url}`}
          />
          <CardParameters data={data?.table_5_1_result} />
        </ResultWrapper>

        {/* table_5_2 */}
        <ResultWrapper>
          <CardPhoto
            photoLink={`${BACKEND_URL}/presigned-url/${data?.table_5_2_url}`}
          />
          <CardParameters data={data?.table_5_2_result} />
        </ResultWrapper>
      </CardsWrapper>
    </MainWrapper>
  );
};
