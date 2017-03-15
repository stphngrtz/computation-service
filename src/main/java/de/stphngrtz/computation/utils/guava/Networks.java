package de.stphngrtz.computation.utils.guava;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

public class Networks {

    public static <N, E> MutableNetwork<N, E> newNetwork() {
        return new ConfigurableMutableEquivalentNetwork<>(NetworkBuilder.directed().build());
    }

    public static boolean equivalent(MutableNetwork<?, ?> networkA, MutableNetwork<?, ?> networkB) {
        return com.google.common.graph.Graphs.equivalent(networkA, networkB);
    }
}
