import { SetterOrUpdater } from "recoil";
import { StockCardType, KeywordCardType } from "../../stores/LaboratoryAtoms";
import DndCard from "./DndCard";
import Collapse from "@mui/material/Collapse";
import styled from "styled-components";

interface Props {
  type: string;
  items: StockCardType[] | KeywordCardType[];
  openState: boolean;
  setOpenState: SetterOrUpdater<boolean>;
}

const AccordionLayout = ({ type, items, openState, setOpenState }: Props) => {
  const headerText = type === "STOCK" ? "종목" : "키워드";

  return (
    <PanelLayout>
      <HeaderWrapper>{headerText}</HeaderWrapper>
      <Collapse in={openState} timeout={500}>
        <ContentWrapper>
          {items.map((item) => {
            return <DndCard key={item.id} item={item} type={type} />;
          })}
        </ContentWrapper>
      </Collapse>
      <TailWrapper>
        <ChevonWrapper
          src={"labImages/chevon.png"}
          alt=""
          rotate={openState}
          onClick={() => setOpenState((prev) => !prev)}
        />
      </TailWrapper>
    </PanelLayout>
  );
};

export default AccordionLayout;

const PanelLayout = styled.div`
  width: 100%;
  background: #f8f8f8;
  border-radius: 24px;

  padding: 18px 0px 12px 0;

  box-shadow: 0px 4px 8px 3px rgba(0, 0, 0, 0.15),
    0px 1px 3px rgba(0, 0, 0, 0.3);
`;

const HeaderWrapper = styled.div`
  padding: 0 30px 18px 30px;
  font-size: 2.2rem;
  font-weight: 700;
`;

const ContentWrapper = styled.div`
  width: 100%;
  height: 400px;
  overflow: auto;

  padding: 12px 18px;

  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 2.5rem;
`;

const TailWrapper = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 12px;
`;
const ChevonWrapper = styled.img<{ rotate: boolean }>`
  transform: ${(props) => (props.rotate ? "scaleY(-1)" : null)};
  transition: 0.5s;
  cursor: pointer;
`;
