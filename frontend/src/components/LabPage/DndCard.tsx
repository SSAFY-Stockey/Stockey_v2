// drag and drop 용 card

import { memo } from 'react';
import { useSetRecoilState } from "recoil";
import {
  StockCardType,
  KeywordCardType,
  draggedLabCardState,
} from "../../stores/LaboratoryAtoms";
import { useDrag } from "react-dnd";

import StockCardMini from "./StockCardMini";
import KeywordCardMini from "./KeywordCardMini";
import styled from "styled-components";

interface ParamProps {
  type: string;
  item: StockCardType | KeywordCardType;
  size?: string;
}

const DndCard = ({ type, item, size = "100px" }: ParamProps) => {
  const setCard = useSetRecoilState(draggedLabCardState);

  //isDragging:  아이템이 드래깅 중일때 true, 아닐때 false. 드래깅 중인 아이템을 스타일링 할때 사용 (opactiy)
  //dragRef는 리액트의 useRef처럼 작동한다. 드래그될 부분에 선언
  //previewRef는 드래깅될때 보여질 프리뷰 이미지
  const [{ isDragging }, dragRef, previewRef] = useDrag(
    () => ({
      type,
      item,
      collect: (monitor) => ({
        //isDragging 변수가 현재 드래깅중인지 아닌지를 리턴
        isDragging: monitor.isDragging(),
      }),
      end: (item, monitor) => {
        // 드래그가 끝났을때 recoil의 draggedCard 변경
        const dropResult: { dropEffect: string; name: string } | null =
          monitor.getDropResult();
        if (dropResult) {
          setCard({ type, item });
        }
      },
    }),
    [item]
  );

  return (
    <DragWrapper ref={dragRef} opacity={`${isDragging ? 0.4 : 1}`} size={size}>
      {type === "STOCK" ? (
        <StockCardMini item={item} />
      ) : (
        <KeywordCardMini item={item} />
      )}
    </DragWrapper>
  );
};

export default memo(DndCard);

const DragWrapper = styled.div<{ opacity: string; size: string }>`
  width: ${(props) => props.size};
  height: ${(props) => props.size};
  background-color: white;

  border-radius: 24px;
  opacity: ${(props) => props.opacity};

  cursor: pointer;
`;
