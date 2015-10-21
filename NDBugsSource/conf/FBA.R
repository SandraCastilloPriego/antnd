
FBA <- function(input, objectName, boundsFile, sourcesFile, compound){ 
  library(glpkAPI)
  library(sybilSBML)
  library(sybil)
  
  ## Open model
  #model <- load(file = objectName)
  #if(is.na(model)){
      print("reading model")
      print(input)
      model <- readSBMLmod(input,"Metabolic Model", validateSBML=FALSE)
      
    #   ## Adding sources
    #   modelPointer = openSBMLfile(input)
    #   id <- getSBMLSpeciesList(getSBMLmodel(modelPointer, ptrtype = "sbml_mod"))[[1]] 
    #   sources <- read.csv(sourcesFile, header= FALSE, sep="\t")
    #   for(i in 1:nrow(sources)){
    #     if(any(id %in% sources[i,1])){
    #       if(is.null(checkReactId(model, paste("Ex_",sources[i,1],sep="")))){
    #         model <- addExchReact(model, sources[i,1], sources[i,2], sources[i,3])  
    #       }else{
    #         model <- changeBounds(model, paste("Ex_",sources[i,1],sep=""), lb = sources[i, 2], ub = sources[i,3])
    #       }
    #     }
    #   }   
    #   
    
      ## Adding bounds
      print("reading bounds")
      print(boundsFile)
      bounds <- read.csv(boundsFile, header= FALSE, row.names=1)  
      rid <- react_id(model)
      
      for(i in 1:length(rid)){    
        if(!is.na(bounds[rid[i],3])){
          model <- changeBounds(model, rid[i], lb = bounds[rid[i],3] ,ub = bounds[rid[i],4] )
        }else{
          model <- changeBounds(model, rid[i], lb = -1000 ,ub = 1000)      
        }
      }  
  #   save(model, file=objectName)
  #}
  
#   ## Adding everything as an exchange reaction   
#   for(i in 1:length(id)){
#     if(!any(sources[,1] %in% id[i]) && !compound %in% id[i]){
#       model <- addExchReact(model,id[i], 0, 1000)
#     }
#   }  
#   
  solutions <- data.frame()
  
  newModel <- model
  newModel <- addReact(newModel, paste("Biomass", compound ,sep="_"), c(compound, 'biomass'),c(-1, 1), FALSE, 0,1000, obj=0)
  newModel <- addReact(newModel,'growth','biomass',-1, FALSE, 0, 1000,obj = 1);
  opt <- optimizeProb(newModel, algorithm = "fba", retOptSol = TRUE)  
  print(opt)
  mtf <- optimizeProb(newModel, algorithm = "mtf", retOptSol = TRUE, wtobj = mod_obj(opt))
  solutions[1,1]<-opt@lp_obj
  if(mtf@lp_ok==0){
    solutions[1,2]<-"solution process was successful"
  }else{
    solutions[1,2]<-mtf@fluxdist@fluxes[nrow(mtf@fluxdist@fluxes)]
  }
  solutions[1,3]<-compound
  if(mtf@lp_stat==4){
    solutions[1,4]<-"no feasible solution exists"
  }else if(mtf@lp_stat==5){
    solutions[1,4]<- "Optimal solution found"
  }else{
    solutions[1,4]<- mtf@lp_stat
  }
  id <- react_id(newModel)
  solutions[1,5]<- compound
  fluxes<- cbind(getFluxDist(mtf), id)  
  return(list(opt,fluxes))
}
