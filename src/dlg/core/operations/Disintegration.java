/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.refinement.IncrementalRefinementOperator;
import dlg.core.refinement.RefinementOperator;
import dlg.core.subsumption.Subsumption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class Disintegration {
    public static int DEBUG = 0;
    
    static HashMap<DLG, List<DLG>> cache = new HashMap<>();

    
    public static List<DLG> greedyDisintegrationWithCache(DLG g, Subsumption s, RefinementOperator rho) throws Exception  {
        List<DLG> l = cache.get(g);
        if (l!=null) return l;
        l = greedyDisintegration(g, s, rho);
        cache.put(g, l);
        return l;
    }
    
    
    public static List<DLG> greedyDisintegration(DLG g, Subsumption s, RefinementOperator rho) throws Exception  {
        List<DLG> properties = new ArrayList<>();
        
        List<DLG> path = new ArrayList<>();
        path.add(g);
        while(true) {
            if (DEBUG>=2) System.out.println("Disintegration.greedyDisintegration: next element in path:\n" + g);
            if (rho instanceof IncrementalRefinementOperator) {
                ((IncrementalRefinementOperator) rho).setDLGForUpwardRefinement(g);
                g = ((IncrementalRefinementOperator) rho).getNextUpwardRefinement();
                if (g==null) break;
                path.add(g);
            } else {
                List<? extends DLG> refinements = rho.upwardRefinements(g);
                if (refinements == null || refinements.isEmpty()) break;
                g = refinements.get(0);
                path.add(g);
            }
        }
        
        for(int i = 0;i<path.size()-1;i++) {
            if (DEBUG>=1) System.out.println("Disintegration.greedyDisintegration: current position in the path:\n" + path.get(i));
            DLG property = reminder(path.get(i), path.get(i+1), s, rho);
            if (DEBUG>=1) System.out.println("Disintegration.greedyDisintegration: next property:\n" + property);
            properties.add(property);
        }
        
        return properties;
    }
    
    
    // Returns the most general term that when unified with `general', returns `specific'
    public static DLG reminder(DLG specific, DLG general, Subsumption s, RefinementOperator rho) throws Exception {
        DLG current = specific;
        List<? extends DLG> generalizations_of_specific = rho.upwardRefinements(specific);
        List<DLG> generalizations_of_specific_subsumed_by_general = new ArrayList<>();
        for(DLG tmp:generalizations_of_specific) {
            if (s.subsumes(general, tmp)!=null) generalizations_of_specific_subsumed_by_general.add(tmp);
        }
        if (DEBUG>=1) System.out.println("Disintegration.reminder: generalizations_of_specific_subsumed_by_general = " + generalizations_of_specific_subsumed_by_general.size());
        
        while(true) {
            DLG next = null;
            List<? extends DLG> refinements = rho.upwardRefinements(current);
            //System.out.println(current);
            if (DEBUG>=1) System.out.println("Disintegration.reminder: " + refinements.size() + " refinements");
            for(DLG candidate:refinements) {
                if (s.subsumes(candidate, general)!=null) {
//                    System.out.println("  subsumes general");
                    continue;
                }
                
                // now we want to check whether U(current,general) = specific
                boolean isunification = true;
                // this can never happen, so, no need to check:
                // if (s.subsumes(candidate, specific)==null) continue;
                // if (s.subsumes(general, specific)==null) continue;
                for(DLG tmp:generalizations_of_specific_subsumed_by_general) {
                    if (s.subsumes(candidate, tmp)!=null) {
//                        System.out.println("  not proper unification");
                        isunification = false;
                        break;
                    }
                }
                
                if (isunification) {
                    next = candidate;
                    break;
                }   
            }
            if (next==null) return current;
            current = next;
        }
    }    
}
