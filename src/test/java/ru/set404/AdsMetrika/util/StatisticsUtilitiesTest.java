package ru.set404.AdsMetrika.util;

import org.junit.jupiter.api.Test;
import ru.set404.AdsMetrika.dto.ChartDTO;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsUtilitiesTest {

    @Test
    public void createStatsDTOTest() {
        int offerId1 = 12345;
        int offerId2 = 54321;
        NetworkStats networkStats = new NetworkStats(11, 100);

        Map<Integer, AdcomboStats> adcomboStatsMap = new HashMap<>();
        AdcomboStats adcomboStats1 = getAdcomboStats();
        AdcomboStats adcomboStats2 = getAdcomboStats();
        adcomboStatsMap.put(offerId1, adcomboStats1);
        adcomboStatsMap.put(offerId2, adcomboStats2);

        StatDTO testingStatDTO = StatisticsUtilities.createStatsDTO(offerId1, networkStats, adcomboStatsMap);
        assertEquals(10, testingStatDTO.getApproveCount());
        assertEquals(100, testingStatDTO.getHoldCost());
        assertEquals(100, testingStatDTO.getROI());
        assertEquals(200 - 100, testingStatDTO.getProfit());
    }

    @Test
    public void createStatsDTOTestWithNetworkStatsMap() {
        int offerId1 = 12345;
        int offerId2 = 54321;

        NetworkStats networkStats1 = new NetworkStats(11, 100);
        NetworkStats networkStats2 = new NetworkStats(22, 200);
        Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
        networkStatsMap.put(offerId1, networkStats1);
        networkStatsMap.put(offerId2, networkStats2);

        Map<Integer, AdcomboStats> adcomboStatsMap = new HashMap<>();
        AdcomboStats adcomboStats1 = getAdcomboStats();
        AdcomboStats adcomboStats2 = getAdcomboStats();
        adcomboStatsMap.put(offerId1, adcomboStats1);
        adcomboStatsMap.put(offerId2, adcomboStats2);

        StatDTO testingStatDTO = StatisticsUtilities.createStatsDTO(offerId1, networkStatsMap, adcomboStatsMap);
        assertEquals(10, testingStatDTO.getApproveCount());
        assertEquals(100, testingStatDTO.getHoldCost());
        assertEquals(100, testingStatDTO.getROI());
        assertEquals(200 - 100, testingStatDTO.getProfit());
    }

    @Test
    public void sumStatDTOTest() {
        StatDTO statDTO1 = new StatDTO(12345, "Test", 11, 22.0, 33.0,
                44, 55.0);
        StatDTO statDTO2 = new StatDTO(54321, "Test 2", 11, 22.0, 33.0,
                44, 55.0);

        StatDTO testingStatDTO = StatisticsUtilities.sumStatDTO(statDTO1, statDTO2);
        assertEquals(110 - 44, testingStatDTO.getProfit());
        assertEquals(88, testingStatDTO.getApproveCount());
        assertEquals(12345, testingStatDTO.getCampaignId());
        assertEquals("Test", testingStatDTO.getCampaignName());
        assertEquals(22, testingStatDTO.getClicks(), 22);
        assertEquals(66, testingStatDTO.getHoldCost());
    }

    @Test
    public void combineTableDTOTest() {
        List<TableDTO> tableStats = getTableDTOList();
        TableDTO testingTableDTO = StatisticsUtilities.combineTableDTO(tableStats);
        assertEquals(44, testingTableDTO.getTotalClicks());
        assertEquals(132, testingTableDTO.getTotalProfit());
        assertEquals(176, testingTableDTO.getTotalHoldCost());
        assertEquals(2, testingTableDTO.getCurrentStats().size());
    }

    @Test
    public void convertForSingleTableTest() {
        List<TableDTO> tableStats = getTableDTOList();
        TableDTO testingTableDTO = StatisticsUtilities.convertForSingleTable(tableStats);
        assertEquals(8, testingTableDTO.getCurrentStats().size());
    }

    @Test
    public void getTotalChartDTOTest() {
        List<Stat> stats = getStatList();
        ChartDTO testingChartDTO = StatisticsUtilities.getTotalChartDTO(stats);
        assertEquals(200, testingChartDTO.getRevenue());
        assertEquals(200, testingChartDTO.getSpend());
    }

    @Test
    public void convertToChartDTOListTest() {
        List<Stat> stats = getStatList();
        List<ChartDTO> testingChartDTOList = StatisticsUtilities.convertToChartDTOList(stats);
        assertEquals(1, testingChartDTOList.size());
        assertEquals(200, testingChartDTOList.get(0).getSpend());
        assertEquals(200 ,testingChartDTOList.get(0).getRevenue());
        assertEquals(LocalDate.now(), testingChartDTOList.get(0).getCreatedDate());
    }

    @Test
    public void getTotalChartSpendTest() {
        List<Stat> stats = getStatList();
        Map<Network, Double> testingResultMap = StatisticsUtilities.getTotalChartSpend(stats);
        assertEquals(200, testingResultMap.get(Network.TF));
        assertEquals(1, testingResultMap.size());
    }

    private static List<Stat> getStatList() {
        Stat stat1 = new Stat();
        stat1.setId(123);
        stat1.setCreatedDate(LocalDate.now());
        stat1.setNetworkName(Network.TF);
        stat1.setSpend(100);
        stat1.setOwner(new User());
        stat1.setRevenue(100);
        Stat stat2 = new Stat();
        stat2.setId(123);
        stat2.setCreatedDate(LocalDate.now());
        stat2.setNetworkName(Network.TF);
        stat2.setSpend(100);
        stat2.setOwner(new User());
        stat2.setRevenue(100);
        return List.of(stat1, stat2);
    }

    private static AdcomboStats getAdcomboStats() {
        return new AdcomboStats(12345, "TestName", 100, 10, 200);
    }

    private static List<TableDTO> getTableDTOList() {
        StatDTO statDTO1 = new StatDTO(12345, "Test", 11, 22.0, 44.0,
                44, 55.0);
        StatDTO statDTO2 = new StatDTO(54321, "Test 2", 11, 22.0, 44.0,
                44, 55.0);
        TableDTO tableDTO1 = new TableDTO(List.of(statDTO1, statDTO2), Network.TF);
        TableDTO tableDTO2 = new TableDTO(List.of(statDTO1, statDTO2), Network.EXO);
        return List.of(tableDTO1, tableDTO2);
    }


}
