
setup {

    systems {
        // Scrap
        ScrapSystem(SCRAP, {entity ->
            return ATTACK_AVAILABLE.retriever.getOrDefault(entity, 0) > 0 &&
                    SICKNESS.retriever.getOrDefault(entity, 1) == 0;
        })
        useCost(action: ENCHANT_ACTION, res: SCRAP, value: SCRAP_COST, whoPays: "player")
        useCost(action: USE_ACTION, res: SCRAP, value: SCRAP_COST, whoPays: "player")
    }
}
