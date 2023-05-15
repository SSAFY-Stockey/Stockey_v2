import styled from "styled-components"
import Grid from "@mui/material/Grid"
import MyRankingItem from "./MyRankingItem"
import RankingItem from "./RankingItem"
import { PanelTitle } from "../StockDetailPage/SubPanel/KeywordPanel/KeywordPanel"
import { triggerScroll } from "../common/Functions/triggerScroll"
import { useEffect } from "react"

const TraderRankingList = () => {
  // 랭킹 리스트로 스크롤 이동
  useEffect(() => {
    triggerScroll("ranking-list")
  }, [])

  return (
    <RankingSection container rowSpacing={2} id="ranking-list">
      <PanelTitle>이번 주 누적 랭킹 🔥</PanelTitle>
      <StickyGrid
        item
        xs={12}
        children={<MyRankingItem rank={4} name="하은하은" account={367900} />}
      />
      <Grid container xs={12} rowSpacing={3}>
        {Array.from({ length: 100 }).map((_, index) => (
          <RankingItem
            key={index}
            rank={index + 1}
            name="경희경희"
            account={367900}
          />
        ))}
      </Grid>
    </RankingSection>
  )
}

export default TraderRankingList

const RankingSection = styled(Grid)`
  padding: 10% 20%;
`

const StickyGrid = styled(Grid)`
  // 상단에 고정
  position: sticky;
  top: 0;

  // 다른 요소랑 겹치면 다른 요소는 안보이게
  z-index: 1;
  background-color: white;

  padding-bottom: 2rem;
`
