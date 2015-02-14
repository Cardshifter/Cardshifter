package com.cardshifter.core.cardloader;

import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class SimpleCardLoader implements CardLoader<Path> {
    @Override
    public Collection<Entity> loadCards(Path input, ECSGame game, ECSMod mod, ECSResource[] resources, ECSAttribute[] attributes) throws CardLoadingException {
        try (BufferedReader reader = new BufferedReader(new FileReader(input.toFile()))) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new CardLoadingException(e);
        }
    }
}
