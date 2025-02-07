package com.plasstech.lang.c.typecheck;

import java.util.List;
import java.util.Optional;

import com.plasstech.lang.c.common.UniqueId;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Break;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Continue;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.DoWhile;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.For;
import com.plasstech.lang.c.parser.FunDecl;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.VarDecl;
import com.plasstech.lang.c.parser.While;

class LoopLabeler implements Validator {

  @Override
  public Program validate(Program input) {
    return new Program(input.declarations().stream()
        .map(d -> {
          return switch (d) {
            case FunDecl fd -> labelFunction(fd);
            case VarDecl vd -> vd;
            default -> throw new IllegalArgumentException("Unexpected value: " + d);
          };
        })
        .toList());
  }

  private Declaration labelFunction(FunDecl funDecl) {
    Optional<Block> labeledBlock = funDecl.body().map(block -> labelBlock(block));
    return new FunDecl(funDecl.name(), funDecl.funType(), funDecl.paramNames(),
        labeledBlock, funDecl.storageClass());
  }

  private Block labelBlock(Block block) {
    List<BlockItem> labeldItems =
        block.items().stream().map(item -> labelBlockItem(item)).toList();
    return new Block(labeldItems);
  }

  private BlockItem labelBlockItem(BlockItem item) {
    return switch (item) {
      case Declaration d -> d;
      case Statement s -> labelStatement(s);
      default -> throw new IllegalArgumentException("Unexpected block item: " + item);
    };
  }

  private String currentLabel = null;

  private Statement labelStatement(Statement statement) {
    return switch (statement) {
      case Expression e -> e;
      case Return r -> r;
      case If i -> labelIf(i);
      case NullStatement n -> n;
      case Compound c -> new Compound(labelBlock(c.block()));
      case For f -> labelFor(f);
      case While w -> labelWhile(w);
      case DoWhile dw -> labelDoWhile(dw);
      case Break b -> {
        if (currentLabel == null) {
          error("Cannot break outside loop");
        }
        yield new Break(currentLabel);
      }
      case Continue c -> {
        if (currentLabel == null) {
          error("Cannot continue outside loop");
        }
        yield new Continue(currentLabel);
      }
      default -> throw new IllegalArgumentException("Unexpected statement type: " + statement);
    };
  }

  private DoWhile labelDoWhile(DoWhile dw) {
    String oldLabel = currentLabel;
    currentLabel = UniqueId.makeUnique("dowhile");
    DoWhile ndw = new DoWhile(currentLabel, labelStatement(dw.body()), dw.condition());
    currentLabel = oldLabel;
    return ndw;
  }

  private While labelWhile(While w) {
    String oldLabel = currentLabel;
    currentLabel = UniqueId.makeUnique("while");
    While nw = new While(currentLabel, w.condition(), labelStatement(w.body()));
    currentLabel = oldLabel;
    return nw;
  }

  private For labelFor(For f) {
    String oldLabel = currentLabel;
    currentLabel = UniqueId.makeUnique("for");
    For nf = new For(
        currentLabel,
        f.init(),
        f.condition(),
        f.post(),
        labelStatement(f.body()));
    currentLabel = oldLabel;
    return nf;
  }

  private Statement labelIf(If i) {
    Statement then = labelStatement(i.then());
    Optional<Statement> elseStmt = i.elseStmt().map(stmt -> labelStatement(stmt));
    return new If(i.condition(), then, elseStmt);
  }
}
