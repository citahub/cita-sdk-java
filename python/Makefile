PROJ := pycita
TESTS ?= tests  # 测试的目标, 默认是./tests目录
SOL_INPUTS := $(wildcard tests/*.sol)
SOL_OUTPUTS := $(patsubst %.sol,%.bin,$(SOL_INPUTS))

.PHONY: doc upload_doc clean test only lint sol

clean:
	rm -rf docs/_build dist/ src/sol/*.bin

%.bin: %.sol
	cat $^ | sudo docker run -i --rm ethereum/solc:0.4.24 --bin --abi --optimize - > $@

sol: $(SOL_OUTPUTS)

doc:
	cd docs && $(MAKE) html
	cd docs/_build/html && tar -zcvf ../docs.tar.gz . &> /dev/null

upload_doc: doc
	PYTHONPATH=$(PWD)/src python docs/upload_doc.py $(PROJ) SERVICE_API $(PROJ) docs/_build/docs.tar.gz

test: $(SOL_OUTPUTS) # 运行测试
	PYTHONPATH=$(PWD)/src pytest --cov=src -vv $(TESTS)

only: $(SOL_OUTPUTS) # 只运行打了 @pytest.mark.only 注解的测试
	PYTHONPATH=$(PWD)/src pytest -m only --cov=src -vv $(TESTS)

lint:
# 	@mypy --pretty \
# 		 --python-version 3.7 \
# 		 --warn-unused-configs \
# 		 --disallow-subclassing-any \
# 		 --disallow-untyped-calls \
# 		 --disallow-untyped-defs \
# 		 --disallow-incomplete-defs \
# 		 --check-untyped-defs \
# 		 --disallow-untyped-decorators \
# 		 --no-implicit-optional \
# 		 --warn-redundant-casts \
# 		 --warn-unused-ignores \
# 		 --warn-return-any \
# 		 --no-implicit-reexport \
# 	     src/
	@flake8 --ignore=E501 \
	        --show-source \
	        --statistics \
	        src/ tests/