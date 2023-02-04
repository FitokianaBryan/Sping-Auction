package com.example.demo.controllerAPI;


import com.example.demo.ObjectBdd.ManipDb;
import com.example.demo.connex.Connexion;
import com.example.demo.dao.EnchereDao;
import com.example.demo.dao.HistoriqueOffreDao;
import com.example.demo.dao.TokenUserDao;
import com.example.demo.dao.UtilisateurDao;
import com.example.demo.models.Enchere;
import com.example.demo.models.HistoriqueOffre;
import com.example.demo.models.Response;
import com.example.demo.models.TokenUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.List;

@RestController
@RequestMapping("/api/HistoriqueOffre")
@CrossOrigin
public class HistoriqueOffreRestController {
    Connexion con = new Connexion();
    HistoriqueOffreDao hod = new HistoriqueOffreDao();
    Connection con1;
    {
        try {
            con1 = ManipDb.pgConnect("postgres","railway","xdUc1BXEMu9U6UjW8VmL");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    HistoriqueOffreDao ho = new HistoriqueOffreDao();
    @PostMapping("ReEncherir/{idEnchere}")
    public Response ReEncherir(@PathVariable int idEnchere, @RequestHeader("token") String token, @RequestParam("montant") float montant_offre) throws Exception {
        Response response = new Response();
       TokenUserDao tud = new TokenUserDao();
       TokenUser t;
//       con.Resolve();
        if(tud.validTokenUser(token)!=0)
       {
           t = tud.getTokenUser(token);
         float montant_user = new UtilisateurDao().getCompteUser(t.getIdUtilisateur(),con);
         Enchere e = new EnchereDao().getObjetEchere(con,idEnchere);
         if(hod.siUserVendeur(con,idEnchere,t.getIdUtilisateur())==1)
         {
             response.setStatus("401");
             response.setMessage("vous ne pouvez pas participer sur votre propre enchere");
         }
         else
         {
             if(montant_user<montant_offre)
             {
                 response.setStatus("403");
                 response.setMessage("Solde insuffisante");
             }
             else if(montant_offre<=e.getPrixMinimumVente())
             {
                 response.setStatus("501");
                 response.setMessage("solde inferieur au prix minimum vente");
             }
             else {
                 ho.Encherir(con,idEnchere,t.getIdUtilisateur(),montant_offre);
                 ho.setCompteUser(t.getIdUtilisateur(),montant_offre,con);
                 response.setStatus("202");
                 response.setMessage("votre offre a été bien prise en compte");
             }
         }
        }
        else{
            response.setMessage("veuillez dabord vous authentifier");
        }
//        con.CloseSC();
        return response;
    }

    @GetMapping("listeOffre")
    public ResponseEntity<List<HistoriqueOffre>> listeOffre(@RequestParam("idEnchere") int idEnchere) throws Exception
    {
        try {
//            if(con1 == null) {
//                con1 = ManipDb.pgConnect("postgres","railway","9EHRLZ2xGeZ0Vu7ZMuAn");;
//            }
            return new ResponseEntity<List<HistoriqueOffre>>(new HistoriqueOffreDao().ListeOffre(con1,idEnchere), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
//        finally { con1.close(); }
    }


}