import styled from "styled-components";
const isDev = import.meta.env.VITE_DEVELOPMENT;
export const CardParameters = ({ data }) => {
  const CardBody = styled.article`
    position: relative;
    width: 100%;
    min-height: 250px;
    border: 2px solid #778899;
    border-radius: 10px;
    padding: 10px;
    // overflow-y: auto;
  `;

  const StyledRegenerateButton = styled.button`
    position: absolute;
    top: 5px;
    right: 5px;
    font-size: 10px;
    padding: 5px 10px;
  `;

  const WrapperAnswerAndProbability = styled.div`
    display: flex;
    flex-direction: column;
    gap: 5px;
  `;

  const BadProbability = styled.span`
    background-color: #e36b6b;
  `;

  const OkProbability = styled.span`
    background-color: #afe3aa;
  `;

  const checkProbability = (value) => {
    return value > 0.5 ? (
      <>
        <OkProbability>Вероятность</OkProbability>:
        <input placeholder={value} />
      </>
    ) : (
      <>
        <BadProbability>Вероятность</BadProbability>:
        <input placeholder={value} />
      </>
    );
  };
  if (!data) return <CardBody>Нет данных</CardBody>;
  if (isDev) {
    return (
      <CardBody>
        <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
        <div>
          <strong>{data.tableName}</strong>
        </div>
        {Object.entries(data)
          .filter(([key]) => key.startsWith("probabilitiesForColumn_"))
          .map(([column, values]) => (
            <div key={column}>
              <strong>{column}:</strong>
              {values.map((v, idx) => (
                <WrapperAnswerAndProbability key={idx}>
                  <span>
                    Ответ: <input placeholder={v.values[idx].answer} />
                  </span>
                  <span>{checkProbability(v.values[idx].probability)}</span>
                </WrapperAnswerAndProbability>
              ))}
            </div>
          ))}
      </CardBody>
    );
  }
  return (
    <CardBody>
      <StyledRegenerateButton>Regenerate</StyledRegenerateButton>
      <div>
        <strong>{data.tableName}</strong>
      </div>
      {Object.entries(data)
        .filter(([key]) => key.startsWith("probabilitiesForColumn_"))
        .map(([column, values]) => (
          <div key={column}>
            <strong>{column}:</strong>
            {values.map((v, idx) => (
              <WrapperAnswerAndProbability key={idx}>
                <span>
                  Ответ: <input placeholder={v.values[idx].answer} />
                </span>
                <span>{checkProbability(v.values[idx].probability)}</span>
              </WrapperAnswerAndProbability>
            ))}
          </div>
        ))}
    </CardBody>
  );
};
