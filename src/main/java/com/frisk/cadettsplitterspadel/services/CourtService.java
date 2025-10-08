package com.frisk.cadettsplitterspadel.services;

import com.frisk.cadettsplitterspadel.entities.Court;

public interface CourtService {
    Court add(Court court);
    Court update(Court court);
    void  delete(Integer id);
}