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
    top: -10px;
    right: -10px;
    font-size: 10px;
    padding: 5px 10px
  `;

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
                <div key={idx}>
                  Ответ: {v.values[idx].answer}, вероятность:
                  {v.values[idx].probability}
                </div>
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
              <div key={idx}>
                Ответ: {v.answer}, вероятность: {v.probability}
              </div>
            ))}
          </div>
        ))}
    </CardBody>
  );
};
