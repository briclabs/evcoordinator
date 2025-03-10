package net.briclabs.evcoordinator;

import java.io.Serializable;
import java.util.List;

public record ListWithCount<P extends Serializable> (List<P> list, int count) {}
